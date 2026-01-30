# Kubernetes Deployment for BuyNest

This directory contains the Kubernetes manifests to deploy the BuyNest microservices stack.

## Prerequisites

- Kubernetes Cluster (Minikube, Docker Desktop, GKE, etc.)
- `kubectl` configured
- Docker images for the services must be built and available to the cluster.

## Deployment Order

1.  **Infrastructure & Config**:
    Deploy the secrets, config maps, and base infrastructure (Zookeeper, Kafka, Postgres, Redis).
    ```bash
    kubectl apply -f k8s/infra/
    ```
    *Wait for the postgres to be ready before proceeding.*

2.  **Monitoring (Optional)**:
    Deploy Prometheus and Grafana.
    ```bash
    kubectl apply -f k8s/monitoring/
    ```

3.  **Application Services**:
    Deploy the microservices.
    ```bash
    kubectl apply -f k8s/app/
    ```

## Secrets and Configuration

The deployment now uses `ConfigMaps` and `Secrets` to manage environment variables.

- **Secrets** (`k8s/infra/secrets.yaml`): Contains sensitive data like database passwords and Redis passwords.
- **ConfigMap** (`k8s/infra/config-maps.yaml`): Contains non-sensitive configuration like service URLs and shared environment variables.

## Accessing the Application

- **API Gateway**: Exposed via LoadBalancer on port `8080`.
  - Localhost: `http://localhost:8080` (if using Docker Desktop/Local support)
  - Minikube: `minikube service api-gateway`
- **Grafana**: Exposed on port `3000`. Credentials: `admin` / (value in `app-secrets`).
- **Eureka**: Exposed on port `8761`.

## Notes

- **PostgreSQL**: configured as a single instance creating multiple databases (`user_db`, `catalog_db`, etc.) via an init script.
- **Service Discovery**: The application is configured to use Eureka (`http://eureka-server:8761/eureka`).
- **Configuration**: Uses environment variables in deployments. Config Server is included but configuration is primarily driven by env vars in the current setup.
