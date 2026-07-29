# j2k-auto Onboarding Site

Source for the j2k-auto onboarding/documentation website, built with
[MkDocs](https://www.mkdocs.org/) + [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/).

## Structure

```
onboarding/
├── mkdocs.yml            # site config, theme, navigation
├── requirements.txt      # mkdocs + mkdocs-material
├── overrides/
│   └── main.html         # Elms Sans font links
└── pages/                # site content (docs_dir)
    ├── index.md           # Overview
    ├── getting-started.md
    ├── implementation.md
    ├── changelog.md
    ├── contributing.md
    ├── assets/            # logo files used by the site
    └── stylesheets/
        └── extra.css      # flat Picasso-inspired theme + Elms Sans
```

## Preview locally

```bash
cd onboarding
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt

mkdocs serve   # serves at http://127.0.0.1:8000
```

## Build static site

```bash
mkdocs build   # outputs to onboarding/site/
```

## Deploy

Once ready, the site can be published to GitHub Pages with:

```bash
mkdocs gh-deploy --force
```

This pushes the built `site/` output to the `gh-pages` branch of the repository.
