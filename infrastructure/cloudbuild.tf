locals {
  branches       = ["main"]
  branches_regex = "^(${join("|", local.branches)})$"
}

resource "google_storage_bucket" "cloudbuild_artifacts" {
  project                     = var.project_id
  name                        = "${var.project_id}-cloudbuild-artifacts"
  location                    = var.region
  uniform_bucket_level_access = true
  versioning {
    enabled = true
  }
}

resource "google_storage_bucket_iam_member" "cloudbuild_artifacts_iam" {
  bucket = google_storage_bucket.cloudbuild_artifacts.name
  role   = "roles/storage.admin"
  member = "serviceAccount:${local.cloudbuild_service_account}"
}

resource "google_service_account_iam_member" "cloudbuild_terraform_sa_impersonate_permissions" {
  service_account_id = "projects/${var.project_id}/serviceAccounts/${var.terraform_sa_email}"
  role               = "roles/iam.serviceAccountTokenCreator"
  member             = "serviceAccount:${local.cloudbuild_service_account}"
}

resource "google_cloudbuild_trigger" "app_pull_request_trigger" {
  name        = "${var.project_id}-app-pull-request-trigger"
  project     = var.project_id
  description = "Terraform validate and plan, then build application"

  github {
    name  = "repo-name" # update repo name
    owner = "repo-owner" # update owner name

    pull_request {
      branch = local.branches_regex
    }
  }

  substitutions = {
    _ARTIFACT_BUCKET_NAME = google_storage_bucket.cloudbuild_artifacts.name
    _WORKSTREAM_PATH      = "infrastructure"
    _ENV                  = "infrastructure"
  }

  filename = "/infrastructure/templates/cloudbuild-app-pull.yaml"
}

resource "google_cloudbuild_trigger" "app_push_request_trigger" {
  name        = "${var.project_id}-app-push-trigger"
  project     = var.project_id
  description = "Terraform validate, plan, and apply, then build and deploy application"

  github {
    name  = "repo-name" # update repo name
    owner = "repo-owner" # update owner name

    push {
      branch = local.branches_regex
    }
  }

  substitutions = {
    _ARTIFACT_BUCKET_NAME = google_storage_bucket.cloudbuild_artifacts.name
    _WORKSTREAM_PATH      = "infrastructure"
    _ENV                  = "infrastructure"
  }

  filename = "/infrastructure/templates/cloudbuild-app-push.yaml"
}
