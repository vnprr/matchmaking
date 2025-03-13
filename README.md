# Server

## 📁 File Structure



## ⚙️ Components

### 📁 Config Package

#### `MessageConfig.java` class

The `MessageConfig` class is a Spring Boot configuration component that sets the message source for internationalization (i18n). 

This class is used to load localized messages from resource bundles defined by the `messages` base name.

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
- `messages.properties`

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

### 📁 Resources Package

Path: `src/main/resources`

#### createAdmin.yml

Type: YAML file

File used by [`AdminiInitializer.java`](#admininitializerjava-class) class to configure the creation of the first admin account.

Properties read from the file include:
- `createAdmin` - a flag that decides whether an account should be created,
- `adminLogin` - username of the admin account,
- `adminPassword` - the password of the admin account.