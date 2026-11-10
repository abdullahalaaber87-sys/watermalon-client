# Watermalon

This project has two parts:

- **`watermalon-mod/`**: the real Fabric mod for Minecraft 1.21.11 (click GUI, draggable HUD, modules, Spotify). See `watermalon-mod/README.md`.
- **Web preview** (this folder): an interactive browser mockup of the GUI and HUD.

## Get the jar

Push this whole project to a GitHub repository. `.github/workflows/build-mod.yml` builds `watermalon-mod/` automatically; download **watermalon-jar** from the run's Artifacts. You can also download `watermalon-mod.zip` or the pre-built `watermalon-1.0.0.jar` from the preview's Build notes panel.

## Run the web preview

```sh
npm install
npm run dev
```

`npm run build` creates the production build. For browser smoke checks, run `node tests/preview-smoke.mjs` while the dev server is running.
