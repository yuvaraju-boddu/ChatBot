# ChatAI Agent PoC
Simple generative AI Agent using OpenAPI model

Create account with OpenAPI:
> [https://platform.openai.com/)

Generates the API keys and also create a organization
> [https://platform.openai.com/settings/organization/general)

Replace the API KEY(get it from openai dashboard under your profile) in application.properties file
> openai.api.key=YOUR_OPENAI_API_KEY

Enable the model
> [https://platform.openai.com/settings/organization/general)

Replace the model in application.properties file
> openai.model=gpt-4o-mini

## Run Spring Boot application
```
mvn spring-boot:run
```
## CURL command
```
curl --location 'http://localhost:8080/agent/chat' \
--header 'Content-Type: application/json' \
--data '{
    "message": "Can you write a Hello world for java?"
}'
```
## Sample Response 
```
Certainly! Here’s a simple "Hello, World!" program in Java: ```java public class HelloWorld { public static void main(String[] args) { System.out.println("Hello, World!"); } } ``` ### Explanation: - **public class HelloWorld**: This defines a public class named `HelloWorld`. - **public static void main(String[] args)**: This is the main method, which is the entry point of any Java application. - **System.out.println("Hello, World!");**: This line prints "Hello, World!" to the console. ### How to Run: 1. Save the code in a file named `HelloWorld.java`. 2. Open a terminal or command prompt and navigate to the directory where the file is saved. 3. Compile the program with the command: ```bash javac HelloWorld.java ``` 4. Run the compiled program with the command: ```bash java HelloWorld ``` You should see the output: ``` Hello, World! ```
```
