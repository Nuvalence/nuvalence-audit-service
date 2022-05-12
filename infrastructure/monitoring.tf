locals {
  alert_emails = [
    "alert-email@yourdomain.com", # update alert emails
  ]

  dlq_subscriptions = [
    element(values(module.pubsub-dlq.subscriptions), 0).name,
  ]


  filter_pubsub_unacked_message = {
    "metric.type"   = "pubsub.googleapis.com/subscription/oldest_unacked_message_age"
    "resource.type" = "pubsub_subscription"
  }
}

resource "google_monitoring_notification_channel" "email" {
  for_each     = toset(local.alert_emails)
  project      = var.project_id
  display_name = "Email Notification Channel"
  type         = "email"
  labels = {
    email_address = each.value
  }
}

resource "google_monitoring_alert_policy" "dlq-alerts" {
  for_each              = toset(local.dlq_subscriptions)
  project               = var.project_id
  display_name          = "${each.value} oldest unacked message increase"
  combiner              = "OR"
  notification_channels = [for channel in google_monitoring_notification_channel.email : channel.id]
  conditions {
    display_name = "${each.value} Oldest Unacked Message Count Increasing"
    condition_threshold {
      filter          = join(" ", [for k, v in local.filter_pubsub_unacked_message : "${k}=\"${v}\""])
      duration        = "600s"
      comparison      = "COMPARISON_GT"
      threshold_value = 180
      aggregations {
        alignment_period   = "300s"
        per_series_aligner = "ALIGN_MEAN"
      }
      trigger {
        count = 1
      }
    }
  }
}
