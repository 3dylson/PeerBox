## Setup

If you have no idea what JGroups is, check this
[_link_](http://www.jgroups.org/index.html).

### Prerequesites

- Make sure that you have Docker and Docker Compose installed
  - Windows or macOS:
    [Install Docker Desktop](https://www.docker.com/get-started)
  - Linux: [Install Docker](https://www.docker.com/get-started) and then
    [Docker Compose](https://github.com/docker/compose)

First we'll need to use the **gradle build task** to assemble the code into a JAR file,
so we can run it with Docker.

The root directory of the project has a `docker` folder and inside where the 
`docker-compose.yml` is, open the terminal and run: 
```console
docker-compose up
```
Only then run the Main on the IDE:
```
└── src
    └── main
        └── java
            └── pt.ipb.dsys.peerbox
                └── Main.java
```

The folder `boxFile` at the root directory is where all the files will be handled by the application.
