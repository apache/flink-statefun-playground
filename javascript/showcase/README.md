# StateFun JavaScript SDK Showcase

This project is intended for new StateFun users that would like to start implementing their StateFun application functions using JavaScript.
The tutorial is streamlined and split into a few parts which we recommend to go through a specific order, as lay out below.
Each part is demonstrated with some code snippets plus comments to guide you through the SDK fundamentals.

## Prerequisites

- node
- npm
- docker
- docker-compose

## Building the example

```
npm install
npm start
```

## Tutorial Sections

The [showcase.js](showcase/showcase.js) file demonstrates SDK concepts at length, and it is highly recommended
to read through it. 

# Next Steps

The setup you executed in the last part of this tutorial is not how you'd normally deploy StateFun processes
and functions. It's a rather simplified setup to allow you to explore the interaction between
functions and the StateFun processes by setting debugger breakpoints in the function code in your IDE.

We recommend now to take a look at a slightly more realistic setup, using Docker Compose, in the
[Greeter Docker Compose Example](../greeter).
