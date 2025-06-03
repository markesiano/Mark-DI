# Marke-DI: Lightweight Dependency Injection for Java

**Marke-DI** is a lightweight, standalone dependency injection (DI) container for Java applications. It provides a flexible and type-safe way to manage dependencies without relying on external libraries. Key features include support for transient and singleton lifetimes, named service registrations, factory-based instantiation, and generic type resolution using `TypeReference`.

This project is ideal for developers seeking a minimalistic DI solution with full control over dependency management.

## Features
- **Lightweight**: No external dependencies, pure Java implementation.
- **Flexible Registration**: Register services by interface, implementation, or factory, with optional names for multiple implementations.
- **Lifetimes**: Supports `TRANSIENT` (new instance per request) and `SINGLETON` (single instance per application).
- **Generic Support**: Resolves generic types (e.g., `Repository<Product>`) using `TypeReference`.
- **Thread-Safe**: Built with concurrency in mind using `ConcurrentHashMap` and double-checked locking.
- **Configuration Block**: Ensures safe service registration within a `configure` block.
- **Debugging**: Provides methods to inspect registered services and singletons.

## Installation

Marke-DI is available as a Maven package via GitHub Packages.

### Prerequisites
- Java 17 or higher.
- Maven installed.
- A GitHub Personal Access Token (PAT) with `read:packages` permission to access the package.

### Step 1: Configure the Repository
Add the GitHub Packages repository to your project's `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/markesiano/Mark-DI</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
        <releases>
            <enabled>true</enabled>
        </releases>
    </repository>
</repositories>
```
### Step 2: Add the Dependency
Include Marke-DI in your  `pom.xml`:
```xml
<dependency>
    <groupId>com.markesiano</groupId>
    <artifactId>marke-di</artifactId>
    <version>1.0.0</version>
</dependency>
```
### Step 3: Configure Authentication
To download the package, configure your Maven `settings.xml` (located at `~/.m2/settings.xml`) with your GitHub credentials:
```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>your-github-username</username>
            <password>your-personal-access-token</password>
        </server>
    </servers>
</settings>
```
Replace `your-github-username` and `your-personal-access-token` with your GitHub username and PAT.

## Usage
Marke-DI is centered around the `ServiceProvider` interface, implemented by `Provider`. You configure services within a `configure` block and resolve them using `getService`. Below are examples inspired by the `AppConfig` class from the original project.

### Basic Example

Register and resolve a simple service:

```java
import com.markesiano.serviceprovider.interfaces.ServiceProvider;
import com.markesiano.serviceprovider.Provider;


interface MyService {
    void doSomething();
}

class MyServiceImpl implements MyService {
    public void doSomething() {
        System.out.println("Service is working!");
    }
}
public class Main {
    public static void main(String[] args) {
        ServiceProvider provider = new Provider();
        provider.configure(() -> {
            provider.registerSingleton(MyService.class, MyServiceImpl.class);
        });

        MyService service = provider.getService(MyService.class);
        service.doSomething(); // Prints: Service is working!
    }
}
```
### Advanced Example: Configuring Multiple Services
The following example mirrors the structure of `AppConfig`, showing how to configure repositories, mappers, and use cases with named registrations and generic types:
```java
import com.markesiano.serviceprovider.interfaces.ServiceProvider;
import com.markesiano.serviceprovider.Provider;
import com.markesiano.serviceprovider.support.TypeReference;

interface Repository<T> {
    void save(T entity);
}

class ProductRepository implements Repository<Product> {
    public void save(Product product) { /* Save product */ }
}

interface Mapper<I, O> {
    O map(I input);
}

class ProductDTOtoProduct implements Mapper<ProductDTO, Product> {
    public Product map(ProductDTO dto) { return new Product(); }
}

class AddProductsUseCase {
    private final Repository<Product> repository;
    private final Mapper<ProductDTO, Product> mapper;

    public AddProductsUseCase(Repository<Product> repository, Mapper<ProductDTO, Product> mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
}

class ProductDTO {}
class Product {}

public class AppConfig {
    private static final String REPO_PRODUCT = "product";
    private static final String MAPPER_PRODUCT_DTO = "mapperProductDTO";

    public static ServiceProvider configureServices() {
        ServiceProvider provider = new Provider();
        provider.configure(() -> {
            // Register repository
            provider.registerTransient(REPO_PRODUCT, Repository.class, ProductRepository.class);

            // Register mapper
            provider.registerTransient(MAPPER_PRODUCT_DTO, Mapper.class, ProductDTOtoProduct.class);

            // Register use case with factory
            provider.registerTransient(AddProductsUseCase.class, p -> {
                Repository<Product> repo = p.getService(REPO_PRODUCT, new TypeReference<Repository<Product>>() {});
                Mapper<ProductDTO, Product> mapper = p.getService(MAPPER_PRODUCT_DTO, new TypeReference<Mapper<ProductDTO, Product>>() {});
                return new AddProductsUseCase(repo, mapper);
            });
        });
        return provider;
    }

    public static void main(String[] args) {
        ServiceProvider provider = configureServices();
        AddProductsUseCase useCase = provider.getService(AddProductsUseCase.class);
        // Use the use case
    }
}
```
## Key Configuration Patterns

- Named Registrations: Use names (e.g., `"product"`, `"mapperProductDTO"`) to register multiple implementations of the same interface.
- Factories: Use `SupplierWithProvider` for complex instantiation, as shown in the use case registration.
- Generic Types: Use `TypeReference` to resolve generic services (e.g., `Repository<Product>`).
- Configuration Block: All registrations must occur within a `provider.configure(() -> { ... })` block to ensure thread safety.

## Debugging
Inspect registered services and singletons for debugging:
```java
provider.getServices().forEach((key, descriptor) -> {
    System.out.println("Service: " + key + " -> " + descriptor);
});
```




## License

[MIT](https://choosealicense.com/licenses/mit/)

