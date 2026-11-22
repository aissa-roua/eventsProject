output "cluster_name" {
  description = "Nom du cluster EKS"
  value       = aws_eks_cluster.this.name
}

output "cluster_endpoint" {
  description = "Adresse de l'API Kubernetes"
  value       = aws_eks_cluster.this.endpoint
}

output "region" {
  description = "Région AWS"
  value       = var.aws_region
}