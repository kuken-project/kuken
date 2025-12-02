# web-ui

This project is a web user-interface for [Katan](https://github.com/KatanPanel/Katan), for those who prefer clicking buttons rather than writing command lines, written in Typescript and uses Vue 3 as a framework.

Looking for user guide, installation and documentation? Visit our [official website](https://katan.org/).

<img src="https://github.com/KatanPanel/web-ui/assets/24600258/54789254-9dc7-4c71-bc14-f4afe3d552b4" alt="Preview">

## Building for Development

**Clone the project repository**

```
git clone https://github.com/KatanPanel/web-ui.git
```

**Run for local development**

TypeScript, Node and Yarn are needed to run this project.

```
yarn dev
```

Also is needed to configure environment variables, create a `.env.local` file
containing the environment variables needed to run the project locally. See
[environment variables example file](https://github.com/KatanPanel/web-ui/blob/main/.env.example) to get started.

**Building for production**

```
yarn build
```

**Format and lint codebase**

We use ESLint as linter and Prettier as code formatter.

```
yarn lint
```

## License

Katan Web User Interface is under MIT license.
