terraform {
  backend "gcs" {
    bucket = "your-bucket" # update bucket name
    prefix = "bootstrap"
  }
  required_version = ">= 1.0"
  required_providers {
    gcp = {
      source  = "hashicorp/google"
      version = "~> 4.3.0"
    }
  }
}

provider "google" {
  billing_project = "your-billing-project-id" # update billing project id
  project         = var.project_id
  region          = var.region
}

resource "google_project_service" "crm" {
  project = var.project_id
  service = "cloudresourcemanager.googleapis.com"
}

resource "google_project_service" "api" {
  project  = var.project_id
  for_each = toset(var.service_apis)
  service  = each.value
  depends_on = [
    google_project_service.crm
  ]
}

output "services" {
  value       = var.service_apis
  description = "Enabled GCP service APIs"
}
