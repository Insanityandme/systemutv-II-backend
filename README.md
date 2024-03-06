<div align="center">

# MyHappyPlants Backend
[Install](#install) • [Testing](#testing) • [Contact](#contact)

--- 

<img src="https://github.com/Insanityandme/systemutv-II-backend/assets/1380257/02761e82-d7dc-44b5-899a-f41c36b56ffe"/> 


</div>

---
### Table of Contents
- [Introduction](#introduction)
- [Installation](#install)
- [Testing](#testing)
- [Contact](#contact)

# Introduction
MyHappyPlants is a Java desktop application that exists to help you organize and keep track of your plants at home! 
Using Trefles extensive API to get detailed information about your species of plants. 

It was originally an incomplete student project written in Java, using JavaFX for all GUI related code.

We were tasked with deciding whether we should continue improving the product or scrap it and start over from scratch.

We decided to start over from scratch and turn it into a Java based backend API using Javalin, a lightweight web framework for Java and Kotlin.
Our frontend is written in Javascript using React and is located here: https://github.com/Insanityandme/systemutv-II-frontend

# Install
Our application is divided into two seperate parts
+ backend
+ frontend

## Back-end
1. ```git clone https://github.com/Insanityandme/systemutv-II-backend.git```
2. If you are using an IDE such as IntelliJ and Eclipse you need to install the Maven plugin for your environment if it isn't already installed. 
It's most likely already installed.
3. Navigate to the file Javalin.java in src/main/java/se/myhappyplants/javalin/
4. Run Javalin.java and it should look something like this: ![image](https://github.com/Insanityandme/systemutv-II-backend/assets/1380257/03980c76-d2d6-48ba-94a2-3be7ee24764d)
5. Now that the server is running navigate to localhost:7002 to check out our API documentation: ![image](https://github.com/Insanityandme/systemutv-II-backend/assets/1380257/01fa74d0-7c10-41f9-ac39-3c942cc34a25)
6. Make sure the backend server is running while you are trying our frontend.

### Instructions for environment variables
1. In your operating system be it Windows, Linux or Mac, set the environment variable `TREFLE_API_KEY` to your Trefle API key.
2. Now you can use this key in your code by calling `System.getenv("TREFLE_API_KEY")` to get the value of the environment variable.

MACOSX: https://phoenixnap.com/kb/set-environment-variable-mac
WINDOWS: https://phoenixnap.com/kb/windows-set-environment-variable

## Front-end
You can find instructions for the frontend here: https://github.com/Insanityandme/systemutv-II-frontend

# Testing
1. Go into src/main/testing folder
2. If Mockito and Junit is installed (it usually is included in your IDE or will be installed through Maven)
3. Enter JavalinTests.java and run the file.
4. Run all other files except Helper.java
5. Done!
6. It should look something like this depending on your editor: 
![image](https://github.com/Insanityandme/systemutv-II-backend/assets/1380257/be9d35ab-a25d-46b4-9530-bed329ff5aee)


# Contact
You are welcome to contact me at bengtegardbook@gmail.com if you have any questions on how to setup this environment.
There might be some mistakes in our installation guide and I apologize for that if that's the case.
