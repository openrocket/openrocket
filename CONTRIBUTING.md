# Contributing to OpenRocket 🚀
Hi, thank you for your interest in OpenRocket! 😊

I will guide you to contributing to OpenRocket, be it as a developer, tester or any other type of help that will launch - *pun intended* - OpenRocket to the next level.

Before I move on: time is money, so to save you time, get used to how OpenRocket is abbreviated with _OR_.

#### Table Of Contents
[Testing](#testing)
* [Reporting bugs](#reporting-bugs)
* [Suggesting new features](#suggesting-new-features)

[Development](#development)
* [Git workflow](#git-workflow)
* [Commit etiquette](#commit-etiquette)
* [Verified commits](#verified-commits)
* [Pull requests](#pull-requests)

[Translation](#translation)

[Documentation](#documentation)

[Anything else](#anything-else)

## Testing
OpenRocket is not perfect, but we need people to discover and clearly document all of its imperfections. The job of a tester is to discover bugs, formulate new feature requests and to test out software updates. 📝

### Reporting bugs
Please be very concise when you post a new issue. Give a short and appropriate title, preferably with the '[Bug]'-tag in the beginning to indicate a bug.

When explaining the issue, the following elements are important:
* Explain how you expected OpenRocket to behave, and how it behaved instead
* Go through the different steps that you took to (re)create the issue
* Include information about your operating system (e.g. 'macOS Monterey version 12.1') and which version of OpenRocket you are using (e.g. 'the latest unstable branch')
* If applicable, include a Bug Report (preferably in a separate .txt file) of the exception that OpenRocket threw

Providing extra information like a screenshot, a screen recording, the .ork file that produced an error etc. really help understand and solve the issue more quickly.

### Suggesting new features
If you would like to see a new feature implemented in OR, make a new issue for it. Preferably include the tag '[Feature Request]' in the issue's title.

Explain the new feature in detail:
* Which new behavior would you like OR to have
* Why is this new feature important

## Development
Please read our [Developer's Guide](https://openrocket.readthedocs.io/en/latest/dev_guide/development_overview.html). If you still have questions about how to set up your environment, with which issues you should start etc., then don't be afraid to send us a message on [Discord](https://discord.gg/qD2G5v2FAw).

Developing OpenRocket may be daunting at first, but if you keep Google, your IDE's search and debug features, and the other developers as close friends, then you will easily create your first pull request.

If you want to work on a certain issue, you should first communicate that you want to work on that issue. This can be done by commenting on the issue something like 'I would like to work on this issue'. This ensures that no more than one person works on a given issue.

### Git workflow
OpenRocket's default branch is `unstable`. Branch from that, and open your pull request against `openrocket/openrocket:unstable`.

Do **not** commit on `unstable` itself — not on the official repo, and not on your fork. A PR whose commits sit on `unstable` is hard to review and even harder to revert. Always use a dedicated branch.

A typical flow:

1. Fork [openrocket/openrocket](https://github.com/openrocket/openrocket) (the [setup guide](https://openrocket.readthedocs.io/en/latest/dev_guide/development_setup.html) walks through this if you have not done it yet).
2. Clone your fork and add the official repo as `upstream`:

```
git clone https://github.com/<you>/openrocket.git
cd openrocket
git remote add upstream https://github.com/openrocket/openrocket.git
```

3. Update `unstable` from upstream, then create a **dedicated branch** for the issue:

```
git fetch upstream
git checkout unstable
git merge --ff-only upstream/unstable
git checkout -b fix-123-button-color
```

4. Commit on that branch only. Push it to your fork (`git push -u origin fix-123-button-color`) and open a PR against `unstable`.

If you already committed on `unstable` by accident, move the work to a new branch before opening the PR:

```
git checkout -b fix-123-button-color
git push -u origin fix-123-button-color
```

Then reset your local `unstable` to `upstream/unstable`. Do not force-push `unstable` on the official repository.

### Commit etiquette
Please make use of **atomic commits**. This means: don't fix 10 different issues and cram them in one commit. Split up commits into smaller commits that fix only one issue/feature.

For example: I fixed an issue where a button was displayed as red instead of blue, but I also found that there was a typo in a text somewhere else. Then put the button-fix in a one commit, give it an appropriate name, and put the typo-fix in another commit. Atomic commits make it much easier for code reviewers to review the code changes.

**Poor practice** — one commit that mixes unrelated work:

```
[#123] Fix button color, typo, and refactor SaveDesignInfoPanel
```

Reviewers then have to untangle three changes, and `git revert` of one of them is painful.

**Good practice** — one logical change per commit:

```
[#123] Display the save-dialog button as blue
[#123] Fix typo in the save-dialog title
[#2680] Remove copied undo handling from SaveDesignInfoPanel
```

Each commit should still build. Do not leave the tree broken between commits just to split lines.

Also give **useful names** to your commits. A good naming convention of a commit is in the form of '[#{GitHub issue number of the issue you are trying to fix}] {Commit subject}'.

Take the example of fixing the red button from issue #123: '[#123] Display red button as blue'. Mentioning '#123' will also automatically link your pull request to the corresponding issue. The commit subject should be short and precise. It is also very useful to include a git commit message body besides just the commit subject to explain why and how you made that commit.

### Verified commits
GitHub can mark your commits as **Verified** if you sign them with a GPG (or SSH) key. This is not required to contribute, but it helps reviewers trust that the commits came from your account.

1. Create a GPG key if you do not have one: [Generating a new GPG key](https://docs.github.com/en/authentication/managing-commit-signature-verification/generating-a-new-gpg-key)
2. Add the public key to your GitHub account: [Adding a GPG key to your GitHub account](https://docs.github.com/en/authentication/managing-commit-signature-verification/adding-a-gpg-key-to-your-github-account)
3. Tell Git to use it:

```
git config --global user.signingkey <your-key-id>
git config --global commit.gpgsign true
```

`user.email` in Git must match an email on that GitHub key. The `users.noreply.github.com` address is fine if that is the email you added to the key.

You can also [sign commits with an SSH key](https://docs.github.com/en/authentication/managing-commit-signature-verification/signing-commits) instead of GPG. After the public key is on your account, signed commits show as Verified on GitHub.

### Pull requests
Right, you've dug into the codebase, found that one nasty line that caused all your troubles and fixed it. It is now time to push your **feature branch** (not `unstable`) and create a pull request from that branch to `openrocket/openrocket:unstable`. As your PR (Pull Request) text, it is good to have the following structure:

1. Explain briefly which issue that you are trying to solve, e.g. 'This PR solves #123 in which buttons were displayed as red instead of blue' 
2. Next explain what the underlying issue was, e.g. 'The problem was that by default Java swing displays buttons as red.' 
3. Next is how you fixed the issue, e.g. 'Fixed it by overriding the default button color to blue' 

You can take a look at example PR [#979](https://github.com/openrocket/openrocket/pull/979).

## Translation
Both the OpenRocket software and the end-user documentation site are multilingual. The job of a translator is to maintain the existing languages, or to make a new translation of an unlisted language. During the development sometimes new translation keys get added in the English language that are not simultaneously translated to other languages. The translator must therefore check which translation keys are still missing in his/her/their language.

How you can make/edit a translation can be found in the [Developer's Guide](https://openrocket.readthedocs.io/en/latest/dev_guide/contributing_to_translations.html).

## Documentation
Our documentation is hosted on [ReadTheDocs](https://openrocket.readthedocs.io/en/latest/index.html).

## Anything else
Do you have the perfect voice for making OpenRocket tutorials, are you a graphical designer that screams to improve OR's design, or are you the salesman that can grow OR's influence? Then go for it! We highly appreciate any help that we get, in any shape or form. 🙃
