#!/bin/bash

gcloud builds submit --substitutions COMMIT_SHA=$(git rev-parse --short HEAD)
