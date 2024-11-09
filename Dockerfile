FROM ubuntu:latest
LABEL authors="stief"

ENTRYPOINT ["top", "-b"]