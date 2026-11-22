variable "aws_region" {
  description = "Région AWS"
  type        = string
  default     = "us-east-1"
}

variable "cluster_name" {
  description = "Nom du cluster EKS"
  type        = string
  default     = "events-cluster"
}

variable "kubernetes_version" {
  description = "Version de Kubernetes"
  type        = string
  default     = "1.34"
}

variable "role_arn" {
  description = "Rôle IAM du lab, utilisé par le cluster et les nœuds"
  type        = string
  default     = "arn:aws:iam::415142512757:role/LabRole"
}

variable "subnet_ids" {
  description = "Sous-réseaux publics, dans deux zones différentes"
  type        = list(string)
  default     = ["subnet-0780e871365e1df7a", "subnet-0a3c37f021b53d5c2"]
}

variable "node_instance_type" {
  description = "Type des machines EC2 des nœuds"
  type        = string
  default     = "t3.medium"
}

variable "node_desired_size" {
  description = "Nombre de nœuds souhaité"
  type        = number
  default     = 2
}

variable "node_port" {
  description = "Port NodePort du service events-service"
  type        = number
  default     = 30089
}