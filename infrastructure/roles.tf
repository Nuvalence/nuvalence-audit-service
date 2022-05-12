locals {
  cloudbuild_service_account = "${var.project_number}@cloudbuild.gserviceaccount.com"
  pubsub_service_account     = "service-${var.project_number}@gcp-sa-pubsub.iam.gserviceaccount.com"
  cloudbuild_roles = [
    "roles/editor",
    "roles/iam.serviceAccountTokenCreator",
    "roles/iam.serviceAccountUser",
    "roles/pubsub.admin",
    "roles/resourcemanager.projectIamAdmin",
    "roles/run.admin",
    "roles/secretmanager.secretAccessor",
    "roles/storage.admin",
  ]

  admin = [
    "your-admin-email@yourdomain.com" # add admin accounts from GCP IAM
  ]
}

resource "google_project_iam_member" "cloudbuild" {
  for_each = toset(local.cloudbuild_roles)
  project  = var.project_id
  role     = each.value
  member   = "serviceAccount:${local.cloudbuild_service_account}"
}

resource "google_project_iam_member" "editors" {
  for_each = toset(local.admin)
  project  = var.project_id
  role     = "roles/editor"
  member   = "user:${each.value}"
}

resource "google_project_iam_member" "admins" {
  for_each = toset(local.admin)
  project  = var.project_id
  role     = "roles/owner"
  member   = "user:${each.value}"
}

resource "google_service_account" "audit_service_account" {
  project      = var.project_id
  account_id   = "${var.project_id}-run-sa"
  display_name = "user-manager service account for Audit Service"
}

resource "google_service_account_iam_binding" "audit_service_account_binding" {
  service_account_id = google_service_account.audit_service_account.name
  role               = "roles/iam.serviceAccountUser"
  members = flatten([
    "serviceAccount:${google_service_account.audit_service_account.email}",
  ])
}

resource "google_service_account" "acceptance_test_account" {
  project      = var.project_id
  account_id   = "${var.project_id}-test-sa"
  display_name = "user-managed service account for acceptance testing Audit Service"
}

resource "google_cloud_run_service_iam_binding" "binding" {
  location = var.region
  project  = var.project_id
  service  = var.service_name
  role     = "roles/run.invoker"
  members = flatten([
    "serviceAccount:${google_service_account.acceptance_test_account.email}",
  ])
}