resource "google_secret_manager_secret" "secret" {
  project   = var.project
  secret_id = var.secret_name
  replication {
    user_managed {
      replicas {
        location = var.location
      }
    }
  }
}

resource "google_secret_manager_secret_version" "version" {
  secret      = google_secret_manager_secret.secret.id
  secret_data = var.secret_data
}
