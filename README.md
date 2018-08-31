# Code Climate clojure-kibit Engine

[![CircleCI](https://circleci.com/gh/lukaszkorecki/codeclimate-kibit/tree/master.svg?style=svg)](https://circleci.com/gh/lukaszkorecki/codeclimate-kibit/tree/master)

`codeclimate-kibit` is a Code Climate engine that wraps [kibit](https://github.com/jonase/kibit). You can run it on your command line using the Code Climate CLI, or on our hosted analysis platform.

### Installation & Usage

1. If you haven't already, [install the Code Climate CLI](https://github.com/codeclimate/codeclimate).
2. Run `codeclimate engines:enable kibit`. This command both installs the engine and enables it in your `.codeclimate.yml` file.
3. You're ready to analyze! Browse into your project's folder and run `codeclimate analyze`.

### Need help?

For help with kibit, [check out their documentation](https://github.com/jonase/kibit).

If you're running into a Code Climate issue, first look over this project's [GitHub Issues](https://github.com/andrewhr/codeclimate-kibit/issues), as your question may have already been covered. If not, [go ahead and open a support ticket with us](https://codeclimate.com/help).

