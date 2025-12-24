# web-ui

## Building for Development

**Clone the project repository**

```
git clone https://github.com/devnatan/kuken.git
```

**Run for local development**

TypeScript, Node and Yarn are needed to run this project.

```
yarn dev
```

Also is needed to configure environment variables, create a `.env.local` file
containing the environment variables needed to run the project locally.

**Building for production**

```
yarn build
```

**Format and lint codebase**

We use ESLint as linter and Prettier as code formatter.

```
yarn lint
```