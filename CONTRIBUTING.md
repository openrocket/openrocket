# Contributing to OpenRocket 🚀
Hi, thank you for your interest in OpenRocket! 😊

I will guide you to contributing to OpenRocket, be it as a developer, tester or any other type of help that will launch - *pun intended* - OpenRocket to the next level.

Before I move on: time is money, so to save you time, get used to how OpenRocket is abbreviated with _OR_.

#### Table Of Contents
[Testing](#testing)
* [Reporting bugs](#reporting-bugs)
* [Suggesting new features](#suggesting-new-features)

[Development](#development)
* [Commit etiquette](#commit-etiquette)
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
Please read our [Developer's Guide](https://github.com/openrocket/openrocket/wiki/Developer%27s-Guide). If you still have questions about how to set up your environment, with which issues you should start etc., then don't be afraid to send us a message on [Slack](https://join.slack.com/t/openrocket/shared_invite/zt-dh0wtpc4-WmkSK1ysqAOqHa6eFN7zgA).

Developing OpenRocket may be daunting at first, but if you keep Google, your IDE's search and debug features, and the other developers as close friends, then you will easily create your first pull request.

If you want to work on a certain issue, you should first communicate that you want to work on that issue. This can be done by commenting on the issue something like 'I would like to work on this issue'. This ensures that no more than one person works on a given issue.

### Commit etiquette
Please make use of **atomic commits**. This means: don't fix 10 different issues and cram them in one commit. Split up commits into smaller commits that fix only one issue/feature.

For example: I fixed an issue where a button was displayed as red instead of blue, but I also found that there was a typo in a text somewhere else. Then put the button-fix in a one commit, give it an appropriate name, and put the typo-fix in another commit. Atomic commits make it much easier for code reviewers to review the code changes.

Also give **useful names** to your commits. A good naming convention of a commit is in the form of '[#{GitHub issue number of the issue you are trying to fix}] {Commit subject}'.

Take the example of fixing the red button from issue #123: '[#123] Display red button as blue'. Mentioning '#123' will also automatically link your pull request to the corresponding issue. The commit subject should be short and precise. It is also very useful to include a git commit message body besides just the commit subject to explain why and how you made that commit.

### Pull requests
Right, you've dug into the codebase, found that one nasty line that caused all your troubles and fixed it. It is now time to push your code and create a pull request of the branch from your own repository to the official repository. As your PR (Pull Request) text, it is good to have the following structure:

1. Explain briefly which issue that you are trying to solve, e.g. 'This PR solves #123 in which buttons were displayed as red instead of blue' 
2. Next explain what the underlying issue was, e.g. 'The problem was that by default Java swing displays buttons as red.' 
3. Next is how you fixed the issue, e.g. 'Fixed it by overriding the default button color to blue' 
4. Finally, for other people to test your code, it is good to include a jar file of your fixed OpenRocket. This can be done using ant ([more info here](https://github.com/openrocket/openrocket/wiki/Instructions-to-Build-from-Terminal)). You can upload this jar-file to e.g. Dropbox or Google Drive and include it in your PR, e.g. 'Here is a jar file for testing: ' or if you're really GitHub-savvy, you can add a hyperlink to the 'jar file'-text. If necessary, you can also include information on how to recreate the original issue so that testers can check whether your code solved the issue. If needed, you can also included information about the expected behavior so that others know what your solution should do.

You can take a look at example PR [#979](https://github.com/openrocket/openrocket/pull/979).

## Translation
Both the OpenRocket software and the end-user documentation wiki site are multilingual. The job of a translator is to maintain the existing languages, or to make a new translation of an unlisted language. During the development sometimes new translation keys get added in the English language that are not simultaneously translated to other languages. The translator must therefor check which translation keys are still missing in his/her/they language.

How you can make/edit a translation can be found on [this site](http://openrocket.trans.free.fr/index.php?lang=en) or the [GitHub wiki](https://github.com/openrocket/openrocket/wiki/Instructions-for-translators).

## Documentation
We have two main documentation channels: a [GitHub wiki](https://github.com/openrocket/openrocket/wiki) and an [OpenRocket wiki page](http://wiki.openrocket.info/Main_Page), both of which require regular updates to keep up to date with the latest developments in the software.

## Anything else
Do you have the perfect voice for making OpenRocket tutorials, are you a graphical designer that screams to improve OR's design, or are you the salesman that can grow OR's influence? Then go for it! We highly appreciate any help that we get, in any shape or form. 🙃

