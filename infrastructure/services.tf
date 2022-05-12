##  List the services needed for the project. 
##  $ gcloud services list --available


variable "service_apis" {
  description = "List of service api's to enable"
  type        = list(any)
  default = [
    "cloudapis.googleapis.com",         # Google Cloud APIs
    "cloudbuild.googleapis.com",        # Cloud Build API
    "compute.googleapis.com",           # Compute Engine API
    "iam.googleapis.com",               # Identity and Access Management (IAM) API
    "logging.googleapis.com",           # Cloud Logging API
    "monitoring.googleapis.com",        # Cloud Monitoring API
    "oslogin.googleapis.com",           # Cloud OS Login API
    "run.googleapis.com",               # Cloud Run Admin API
    "secretmanager.googleapis.com",     # Secret Manager API
    "servicenetworking.googleapis.com", # Service Networking API
    "serviceusage.googleapis.com",      # Service Usage API
    "sqladmin.googleapis.com",          # Cloud SQL Admin API
    "vpcaccess.googleapis.com",         # Serverless VPC Access API
  ]
}
