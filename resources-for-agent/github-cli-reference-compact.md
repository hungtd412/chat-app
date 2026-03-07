# GitHub CLI Examples (Compact Reference)

Source: `resources-for-agent/Examples _ GitHub CLI.html`

## Scope
This compact note captures practical `gh` command patterns for:
- checking out pull requests
- cloning repositories
- creating issues and pull requests
- forking repositories
- listing/filtering issues and PRs
- checking status
- viewing items in terminal or browser

## 1) Check out a pull request locally

```bash
gh pr checkout 12
gh pr checkout branch-name
```

## 2) Clone a repository

```bash
gh repo clone cli/cli
gh repo clone https://github.com/cli/cli
```

## 3) Create pull requests and issues

```bash
gh pr create
gh issue create
gh pr create --title "Pull request title" --body "Pull request body"
gh issue create --title "Issue title" --body "Issue body"
gh pr create --web
gh issue create --web
```

## 4) Fork repositories

```bash
gh repo fork
gh repo fork cli/cli
gh repo fork --remote=false
```

## 5) List issues / pull requests

```bash
gh pr list
gh issue list
gh pr list --state closed --assignee user
gh issue list --state closed --assignee user
```

## 6) Check status

```bash
gh pr status
gh issue status
```

## 7) View PR / issue / repository

```bash
gh pr view 21
gh issue view 21
gh repo view owner/repo
gh pr view 21 --web
gh issue view 21 --web
gh repo view owner/repo --web
gh pr view
gh repo view
```
