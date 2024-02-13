{
  "apiVersion": "v1",
  "kind": "Service",
  "metadata": {
    "name": "{pg-arc-executor-label}",
    "labels": {
      "app": "{pg-arc-executor-label}"
    }
  },
  "spec": {
    "type": "ClusterIP",
    "ports": [
      {
        "port": {port}
      }
    ],
    "selector": {
      "app": "{pg-arc-executor-label}"
    }
  }
}