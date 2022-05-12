variable "project_id" {}
variable "region" {}
variable "tier" {}
variable "private_net_self_link" {}
variable "database_name" {}
variable "database_user" {}
variable "database_pass" {}
variable "ip_configuration" {}
variable "replica_count" {}
variable "max_connections" {
  default = 50
}
