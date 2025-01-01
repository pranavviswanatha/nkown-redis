# Redis-like Implementation in Java

This project is a **learning exercise** to implement a basic Redis-like server using Java. It's not intended for production use but serves as a fun way to explore concepts like in-memory data storage, command parsing, thread safety, and networking.

---

## Features

- **PING**: Responds with "PONG" or a custom message.
- **SET**: Stores a key-value pair.
- **GET**: Retrieves the value of a key.
- **HSET**: Stores a field-value pair in a hash.
- **HGET**: Retrieves the value of a field in a hash.

This implementation uses a basic in-memory store and supports thread-safe operations using `ReadWriteLock`.

Based on the blog: www.build-redis-from-scratch.dev/en

---

## Getting Started

### Prerequisites

- Java 11 or later
- Maven (for building the project)

### Clone and Build

1. Clone the repository:
   ```bash
   git clone https://github.com/pranavviswanatha/nkown-redis.git
   cd nkown-redis
