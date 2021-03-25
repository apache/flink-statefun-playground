#!/bin/bash

kubectl logs -f -l component=functions -n statefun
