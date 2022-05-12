output "master_connection" {
  value     = google_sql_database_instance.master-instance.connection_name
  sensitive = true
}

output "database_name" {
  value     = google_sql_database.database.name
  sensitive = true
}

output "master_instance_name" {
  value = google_sql_database_instance.master-instance.name
}
