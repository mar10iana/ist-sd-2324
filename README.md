# TupleSpaces - Distributed Systems Project 2023/2024

**Achievement**: This project was awarded a grade of  out of 20.

## Introduction

This repository hosts the implementation of the TupleSpaces project for the Distributed Systems course at Instituto Superior Técnico, academic year 2023/2024.
The TupleSpaces project aims to develop a distributed TupleSpace service, utilizing gRPC and Java. The service enables users (workers) to add, read, and remove tuples in a shared space. A tuple is an ordered set of fields represented as a string, e.g., `"<vaga,sd,turno1>"`. The system allows for the existence of identical tuple instances and supports searching for tuples using regular expressions.

The user operations supported are *put*, *read*, *take*, and *getTupleSpacesState*.

## Project Variants

The project is divided into three variant, illustrating different ways to implement the service. Each variant presents a unique server interface and ClientService file due to the distinct processing of requests.

### Variant R1: Single Server

Development of a single-server solution, accepting requests at a fixed address/port.

#### Steps:

- **1.1**: Server's port is predefined and known to all clients.
- **1.2**: Clients dynamically discover the server address through a naming server implemented in Python.

### Variant R2: Replication

An alternative solution where the service is replicated across three servers (A, B, C), following the Xu and Liskov algorithm. Clients discover server addresses dynamically via the naming server.

#### Steps:

- **2.1**: Develop *read* and *put* operations without supporting *take* initially.
- **2.2**: Implement the *take* operation as per the Xu/Liskov algorithm, including the necessary code for its two-step execution.

### Variant 3: State Machine Replication

Development of a variant based on the State Machine Replication approach as an alternative to the Xu/Liskov algorithm.

#### Step:

- **3.1**: Clients invoke *put* or *take* operations by first contacting a remote service for a unique sequence number, then sending the request to TupleSpace servers along with this number.
