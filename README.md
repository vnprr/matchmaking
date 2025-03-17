# Server

This repository contains the server-side code for the Matchmaking - an application to operate premium matrimonial services.

## 📁 File Structure



```
Server/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── matchmaking/
│       │           └── backend/
│       │               ├── config/
│       │               │   └── MessageConfig.java
│       │               ├── controller/
│       │               │   ├── AdminInitializer.java
│       │               │   ├── JwtRequestFilter.java
│       │               │   ├── JwtUtil.java
│       │               │   ├── UserDetailsServiceImpl.java
│       │               │   └── SecurityConfig.java
│       │               ├── model/
│       │               │   ├── User.java
│       │               │   └── UserDTO.java
│       │               └── service/
│       │                   └── MessageService.java
│       └── resources/
│           ├── application.properties
│           ├── createAdmin.yml
│           └── messages.properties
```

## ⚙️ Components

### 📁 Config Package

#### `MessageConfig.java` class

The `MessageConfig` class is a Spring Boot configuration component that sets the message source for internationalization (i18n). 

This class is used to load localized messages from resource bundles defined by the [`messages`](#messagesproperties) base name.

Uses **UTF-8** encoding.

Annotations: 
- `@Configuration`
    Allows Spring Boot to automatically detect and register its declared beans.

Methods:
- `messageSource()`
  - Annotation:  `@Bean`
  - Creates a `ReloadableResourceBundleMessageSource` object that loads messages from the `messages` base name. This object is used to set the message source for internationalization.
  - Returns: `ReloadableResourceBundleMessageSource` object.

Related files: 
- [`messages.properties`](#messagesproperties)

### 📁 Controller Package

#### AdminInitializer.java class

The `AdminInitializer` class is a Spring Boot component that implements the `CommandLineRunner` interface. This ensures that the initialization operations performed are run automatically at application startup.

##### Main tasks of the class

- Initialization of the administrator account: 
  The class is responsible for creating an administrator account in the database, if it does not already exist.
- **Configuration from YAML file:**.  
  The configuration for creating an admin account is taken from the `createAdmin.yml`
- **Password security:**.  
  Before saving to the database, the password is encrypted using an injected `PasswordEncoder` component.

##### Implementation details
- **One-time operation:**.  
  Initialization of the admin account occurs only if the account with the specified name does not exist. Thus, even if this operation is performed every time the server is started, the admin account will be created only once.
- **Integration with the rest of the system:**.  
  Is part of the application security system, in

#### JwtRequestFilter

#### JwtUtil

#### MessageConfig

#### UserDetailsServiceImpl

#### SecurityConfig

--------------------------------------

### 📁 Resources Package

Path: `src/main/resources`

#### createAdmin.yml

Type: YAML file

File used by [`AdminiInitializer.java`](#admininitializerjava-class) class to configure the creation of the first admin account.

Properties read from the file include:
- `createAdmin` - a flag that decides whether an account should be created,
- `adminLogin` - username of the admin account,
- `adminPassword` - the password of the admin account.

#### messages.properties

--------------------------------------

### 📁 Model Package

#### 👤 User
User

#### 🤵 UserDTO
DTO (Data Transfer Object) is a simple object designated to transport data between layers of the application.

--------------------------------------

### 📁 Services Package
`com/matchmaking/backend/service`

#### 🏳️ Message Service
##### Java class `MessageService.java`
##### Decorators `@Service`

Provides a centralized mechanism to retrieve localized messages from the underlying `MessageSource`. It simplifies message lookups by abstracting the direct calls to the MessageSource while using a default locale.

##### Features

- Uses a default locale obtained via `Locale.getDefault()`.
- Wraps Spring's `MessageSource` for consistent internationalization (i18n).
- Provides a single, reusable method for message retrieval.

##### Usage

1. **Injection:**  
   Inject the `MessageService` into any Spring component (such as controllers or services):

   ```java
   private final MessageService messageService;
   ```

2. Retrieving a Message:
   Call the getMessage method with the message code:

   ```java
    String successMessage = messageService.getMessage("registration.success");
   ```