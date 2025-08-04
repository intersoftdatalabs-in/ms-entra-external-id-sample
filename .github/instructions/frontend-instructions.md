---
applyTo: "frontend/**/*"
---
# GitHub Copilot Instructions for Angular 19 Application

## Project Overview
This is an Angular 19 application focused on building a modular, scalable, and maintainable UI. The project uses TypeScript, follows Angular's reactive programming paradigm with Signals, and adheres to the Google TypeScript Style Guide for consistent code quality.

## Folder Structure
- `src/app/components`: Reusable UI components
- `src/app/services`: Injectable services for business logic
- `src/app/models`: TypeScript interfaces and types
- `src/app/modules`: Feature modules
- `src/app/shared`: Shared components, directives, and pipes
- `src/app/core`: Core services and configurations
- `src/assets`: Static assets (images, JSON, etc.)
- `src/environments`: Environment-specific configurations

## Coding Standards and Conventions
- **Language**: Use TypeScript for all code. Always define explicit types for variables, parameters, and return values. Avoid `any` unless absolutely necessary.[](https://copilot-instructions.md/)
- **Angular 19 Best Practices**:
  - Use `input()`, `output()`, `viewChild()`, `viewChildren()`, `contentChild()`, and `contentChildren()` functions instead of decorators for component inputs, outputs, and queries.[](https://github.com/github/awesome-copilot/blob/main/instructions/angular.instructions.md)
  - Leverage Angular’s Signals for reactive state management where applicable.
  - Use `OnPush` change detection strategy for performance optimization in components with immutable data.
  - Keep templates clean; move complex logic to component classes or services.
  - Use Angular directives and pipes for reusable functionality.
  - Implement server-side rendering (SSR) or static site generation (SSG) with Angular Universal when specified.
- **Google TypeScript Style Guide**:
  - Use `camelCase` for variable and function names, `PascalCase` for class and interface names.
  - Use double quotes (`"`) for string literals.
  - Indent with 2 spaces, no tabs.
  - Always use arrow functions (`=>`) for callbacks and methods in components and services.
  - Prefer `const` over `let`; avoid `var`.
  - Use descriptive variable names (e.g., `userProfile` instead of `up`).
  - Include JSDoc comments for public methods, classes, and interfaces.
  - Avoid magic numbers; define them as named constants.
  - Use `null` for intentional absence of value; avoid `undefined`.
- **Component Structure**:
  - Follow Angular’s component lifecycle hooks best practices (e.g., use `ngOnInit` for initialization).
  - Use component-level CSS encapsulation (`ViewEncapsulation.Emulated` by default).
  - Organize components into feature modules for scalability.
- **Services**:
  - Use the Repository pattern for data access.
  - Inject services at the root level (`providedIn: 'root'`) unless module-specific.
  - Handle HTTP requests with Angular’s `HttpClient` and use `fetch` for non-Angular contexts.[](https://docs.github.com/en/copilot/how-tos/agents/copilot-code-review/configuring-coding-guidelines)
- **Testing**:
  - Write unit tests for components, services, and pipes using Jasmine and Karma.
  - Use Angular’s `TestBed` for component testing with mocked dependencies