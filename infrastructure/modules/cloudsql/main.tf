resource "google_sql_database_instance" "master-instance" {
  project             = var.project_id
  region              = var.region
  database_version    = "POSTGRES_13"
  deletion_protection = false

  settings {
    tier              = var.tier
    activation_policy = "ALWAYS"
    availability_type = "REGIONAL"
    disk_autoresize   = true

    database_flags {
      name  = "max_connections"
      value = var.max_connections
    }

    backup_configuration {
      enabled                        = true
      start_time                     = "00:00"
      transaction_log_retention_days = 7
      backup_retention_settings {
        retention_unit   = "COUNT"
        retained_backups = 7
      }
    }
    ip_configuration {
      ipv4_enabled    = try(var.ip_configuration["ipv4_enabled"], false)
      require_ssl     = try(var.ip_configuration["require_ssl"], null)
      private_network = var.private_net_self_link

      dynamic "authorized_networks" {
        for_each = contains(
          keys(var.ip_configuration), "authorized_networks"
        ) ? var.ip_configuration.authorized_networks : []
        iterator = net
        content {
          name  = net.value["name"]
          value = net.value["value"]
        }
      }

    }
    maintenance_window {
      day          = 7
      hour         = 0
      update_track = "stable"
    }
  }
}

resource "google_sql_database" "database" {
  project  = var.project_id
  name     = var.database_name
  instance = google_sql_database_instance.master-instance.name
}

resource "google_sql_user" "db-user" {
  project         = var.project_id
  name            = var.database_user
  password        = var.database_pass
  host            = null
  instance        = google_sql_database_instance.master-instance.name
  type            = "BUILT_IN"
  deletion_policy = "ABANDON"
  depends_on = [
    google_sql_database_instance.master-instance
  ]
}
