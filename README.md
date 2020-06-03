# PA-2020-Groupe1 Server

### Run the server

You can go fully with docker but its not recommended for development purpose because of 
the Dockerfile that is made for production.

However you can run :
```bash
docker-compose up --build
```
And you will be able to access to the server at http://localhost:8082 with the db running.

For a better development experience just run the server with your favorite IDE and run:
```bash
docker-compose up --build --no-deps -d db
```
Which will run the database in a docker container.