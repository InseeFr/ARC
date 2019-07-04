# ARC - Acquire - Register - Control : Workbench for acquisition and normalization of data sets

*This is a draft version of the open source ARC application. We know that we use a Insee"s libary witch is not open source, and make the application impossible to compile for external use. We are working on it !*

## Running the app



```
docker build -f app.Dockerfile \
  --build-arg HTTP_PROXY=${HTTP_PROXY}  \
  --build-arg HTTPS_PROXY=${HTTPS_PROXY} \
  .
```