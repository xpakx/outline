# Outline

## General info
Clone of outline.com written in Java + Spring (backend) and Typescript + Angular (frontend).

![gif](readme_files/screen.gif)

## Table of contents
* [General info](#general-info)
* [Technologies](#technologies)
* [Setup](#setup)
* [Features](#features)

## Technologies
Project is created with:
* Java
* Spring Boot
* Typescript
* Angular
* PostgreSQL

## Setup
In order to run project locally you need to clone this repository and build project with Docker Compose:

```
$ git clone https://github.com/xpakx/outline.git
$ cd outline
$ docker-compose up --build -d
```

To stop:
```
$ docker-compose stop
```

## Features
- [x] Outlining
	- [x] Extracting title
	- [x] Extracting content
	- [x] Extracting data
- [x] Article page
	- [x] Short links
	- [x] Copying link
	- [x] Annotating
	- [x] Exporting to markdown

