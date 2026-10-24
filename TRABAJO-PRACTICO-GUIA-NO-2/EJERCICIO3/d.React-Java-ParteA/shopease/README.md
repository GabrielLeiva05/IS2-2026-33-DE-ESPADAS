# Getting Started with Create React App

This project was bootstrapped with [Create React App](https://github.com/facebook/create-react-app).

## Available Scripts

In the project directory, you can run:

### `npm start`

Runs the app in the development mode.\
Open [http://localhost:3000](http://localhost:3000) to view it in your browser.

The page will reload when you make changes.\
You may also see any lint errors in the console.

### `npm test`

Launches the test runner in the interactive watch mode.\
See the section about [running tests](https://facebook.github.io/create-react-app/docs/running-tests) for more information.

### `npm run build`

Builds the app for production to the `build` folder.\
It correctly bundles React in production mode and optimizes the build for the best performance.

The build is minified and the filenames include the hashes.\
Your app is ready to be deployed!

See the section about [deployment](https://facebook.github.io/create-react-app/docs/deployment) for more information.

### `npm run eject`

**Note: this is a one-way operation. Once you `eject`, you can't go back!**

If you aren't satisfied with the build tool and configuration choices, you can `eject` at any time. This command will remove the single build dependency from your project.

Instead, it will copy all the configuration files and the transitive dependencies (webpack, Babel, ESLint, etc) right into your project so you have full control over them. All of the commands except `eject` will still work, but they will point to the copied scripts so you can tweak them. At this point you're on your own.

You don't have to ever use `eject`. The curated feature set is suitable for small and middle deployments, and you shouldn't feel obligated to use this feature. However we understand that this tool wouldn't be useful if you couldn't customize it when you are ready for it.

## Learn More

You can learn more in the [Create React App documentation](https://facebook.github.io/create-react-app/docs/getting-started).

To learn React, check out the [React documentation](https://reactjs.org/).

### Code Splitting

This section has moved here: [https://facebook.github.io/create-react-app/docs/code-splitting](https://facebook.github.io/create-react-app/docs/code-splitting)

### Analyzing the Bundle Size

This section has moved here: [https://facebook.github.io/create-react-app/docs/analyzing-the-bundle-size](https://facebook.github.io/create-react-app/docs/analyzing-the-bundle-size)

### Making a Progressive Web App

This section has moved here: [https://facebook.github.io/create-react-app/docs/making-a-progressive-web-app](https://facebook.github.io/create-react-app/docs/making-a-progressive-web-app)

### Advanced Configuration

This section has moved here: [https://facebook.github.io/create-react-app/docs/advanced-configuration](https://facebook.github.io/create-react-app/docs/advanced-configuration)

### Deployment

This section has moved here: [https://facebook.github.io/create-react-app/docs/deployment](https://facebook.github.io/create-react-app/docs/deployment)

### `npm run build` fails to minify

This section has moved here: [https://facebook.github.io/create-react-app/docs/troubleshooting#npm-run-build-fails-to-minify](https://facebook.github.io/create-react-app/docs/troubleshooting#npm-run-build-fails-to-minify)

# ShopEase

ShopEase es una interfaz web de una tienda de ropa desarrollada con **React**. El proyecto está enfocado en la presentación de productos y en la organización de la interfaz mediante componentes reutilizables.

Está basado en el prototipo de figma: https://www.figma.com/design/LuA9ntb3NuubhmPxpx5htn/ShopEase?node-id=109-257

## ¿Cómo funciona?

La aplicación comienza en `index.js`, donde se renderiza el componente principal `Shop`.

La página principal está formada por diferentes componentes:

- **Navigation:** muestra el logo, enlaces de navegación, buscador e íconos de favoritos, cuenta y carrito.
- **HeroSection:** presenta la imagen principal de la tienda y un botón de compra.
- **NewArrivals:** muestra los productos nuevos mediante un carrusel responsive.
- **Category:** genera las categorías de productos a partir de la información de `content.json`.
- **Card:** es un componente reutilizable utilizado para mostrar cada producto o categoría con su imagen, título y descripción.
- **Footer:** muestra información de ayuda, empresa, políticas, ubicación, redes sociales y copyright.

## Datos

La información de las categorías y del footer se encuentra en:

```text
src/data/content.json
```

Esto permite separar los datos de la estructura visual. El componente `Shop` lee las categorías del JSON y genera dinámicamente cada sección.

Los productos de **New Arrivals** están definidos actualmente dentro de `NewArrivals.jsx`.

## Tecnologías utilizadas

- **React 19**
- **JavaScript**
- **Tailwind CSS**
- **React Multi Carousel**
- **Create React App**
- **HTML / CSS**

## Estructura principal

```text
src/
├── components/
│   ├── Navigation/
│   ├── HeroSection/
│   ├── Card/
│   ├── Footer/
│   └── Sections/
├── assets/
│   ├── img/
│   └── fonts/
├── data/
│   └── content.json
├── utils/
│   └── Section.constants.js
├── Shop.jsx
└── index.js
```

## Ejecutar el proyecto

Instalar las dependencias:

```bash
npm install
```

Iniciar el proyecto:

```bash
npm start
```

Luego se puede acceder desde:

```text
http://localhost:3000
```

La idea central del proyecto es **dividir la interfaz en componentes reutilizables**. Por ejemplo, `Card` se utiliza tanto para mostrar productos de "New Arrivals" como para mostrar las diferentes categorías. Además, parte del contenido se obtiene desde un archivo JSON, evitando escribir manualmente cada sección en el componente principal.
