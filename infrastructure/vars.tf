variable "billing_account" {
  default = "your-billing-account-id" # update billing account id
}

variable "org_id" {
  default = "your-org-id" # update org id
}

variable "project_id" {
  default = "your-gcp-project-id" # update project id
}

variable "project_number" {
  default = "your-project-number" # update project number
}

variable "region" {
  default = "your-region" # update gcp region (us-east4)
}

variable "terraform_sa_email" {
  default = "terraform@your-gcp-project-id.iam.gserviceaccount.com" # update with service account email
}

variable "database_name" {
  default = "audit"
}

variable "database_user" {
  default = "audituser"
}

variable "service_name" {
  default = "audit-service"
}