locals {
  audit_service_sa = google_service_account.audit_service_account.email
}

resource "google_container_registry" "registry" {
  project  = var.project_id
  location = "US" # update this to match container location
}

resource "google_storage_bucket_iam_member" "terraform" {
  bucket = google_container_registry.registry.id
  role   = "roles/storage.admin"
  member = "serviceAccount:terraform@audit-service-9446210.iam.gserviceaccount.com"
}

data "google_compute_network" "vpc_network" {
  project = var.project_id
  name    = "default"
}

resource "google_vpc_access_connector" "connector" {
  project        = var.project_id
  name           = "vpcconn"
  provider       = google-beta
  region         = var.region
  ip_cidr_range  = "10.8.0.0/28"
  network        = data.google_compute_network.vpc_network.name
  min_instances  = 2
  max_instances  = 10
  max_throughput = 1000
  machine_type   = "e2-micro"
  depends_on = [
    google_project_service.api,
  ]
}

resource "google_compute_global_address" "private_ip_address" {
  project       = var.project_id
  name          = "private-peering-address"
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  prefix_length = 16
  network       = data.google_compute_network.vpc_network.self_link
}

resource "google_service_networking_connection" "private_vpc_connection" {
  network                 = data.google_compute_network.vpc_network.self_link
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.private_ip_address.name]
}

resource "random_password" "db_pass" {
  length  = 20
  special = true
}

module "db_user_secret" {
  source      = "./modules/secret"
  project     = var.project_id
  secret_name = "db_user"
  location    = var.region
  secret_data = var.database_user
}

module "db_pass_secret" {
  source      = "./modules/secret"
  project     = var.project_id
  secret_name = "db_pass"
  location    = var.region
  secret_data = random_password.db_pass.result
}

module "db_name_secret" {
  source      = "./modules/secret"
  project     = var.project_id
  secret_name = "db_name"
  location    = var.region
  secret_data = var.database_name
}

module "db_connection_secret" {
  source      = "./modules/secret"
  project     = var.project_id
  secret_name = "db_connection"
  location    = var.region
  secret_data = module.postgres.master_connection
}

module "postgres" {
  source                = "./modules/cloudsql"
  project_id            = var.project_id
  region                = var.region
  tier                  = "db-f1-micro"
  replica_count         = 0
  private_net_self_link = data.google_compute_network.vpc_network.self_link
  database_name         = var.database_name
  database_user         = var.database_user
  database_pass         = random_password.db_pass.result
  max_connections       = 100
  ip_configuration = {
    ipv4_enabled = false
    require_ssl  = false
  }
  depends_on = [
    google_project_service.api,
  ]
}

/* placeholder names */
module "pubsub-dlq" {
  source     = "./modules/pubsub"
  project_id = var.project_id
  name       = "your-dlq-topic-audit-events" # update dlq topic name

  iam = {
    "roles/pubsub.publisher" = [
      "serviceAccount:${local.audit_service_sa}",
      "serviceAccount:${local.pubsub_service_account}",
    ]
  }

  subscriptions = {
    dlq-sub-audit-events = null
  }

  subscription_iam = {
    dlq-sub-audit-events = {
      "roles/pubsub.subscriber" = [
        "serviceAccount:${local.audit_service_sa}"
      ]
    }
  }
}

module "pubsub" {
  source     = "./modules/pubsub"
  project_id = var.project_id
  name       = "your-topic-audit-events" # update topic audit events name

  iam = {
    "roles/pubsub.publisher" = [
      "serviceAccount:${local.audit_service_sa}",
    ]
  }

  subscriptions = {
    sub-audit-events = null
  }

  subscription_iam = {
    sub-audit-events = {
      "roles/pubsub.subscriber" = [
        "serviceAccount:${local.audit_service_sa}",
        "serviceAccount:${local.pubsub_service_account}",
      ]
    }
  }

  dead_letter_configs = {
    sub-audit-events = {
      topic                 = module.pubsub-dlq.id
      max_delivery_attempts = 5
    }
  }

  retry_policy = {
    sub-audit-events = {
      minimum_backoff = "10s"
      maximum_backoff = "600s"
    }
  }
}
