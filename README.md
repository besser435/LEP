<!-- (https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax -->

# League of Nations Economy Plugin
LEP is a custom economy plugin for the TEAW Minecraft server. Currently, it adds a shop for items, which
is automatically updated by the LoN. It also sends data about the economy for future analysis (this will be
available soon).

It also includes an undocumented command which may or may not be fun :3

## Download
Head to the [release tab](https://github.com/besser435/LEP/releases) to download the latest jar Currently
it is for Minecraft 1.20 for Spigot.

## Contributing
LoN welcomes community contributions. If you would like to report a bug, feature request, or anything else, open an
[issue](https://github.com/besser435/LEP/issues). Pull requests are welcomed too if you know how to do that.

The plugin is built with Maven, and is edited in IntelliJ IDEA. It is free for students.
[This video](https://www.youtube.com/watch?v=s1xg9eJeP3E) is helpful for getting started. 

## Plugin Configuration
> [!NOTE]
> This plugin is made for the TEAW Minecraft server. As such, it may need additional configuration for your setup. 
> Ensure the options in `config.yml` are correct.

## Notes about automatic plugin updates
> [!IMPORTANT]
> For very convoluted reasons, do NOT change the name of the jar files. This will screw with the
> automatic updates. Spigot does not provide a good way to update plugins, so we need to resort to
> a bit of jank. There is a real explanation in the raw README file.
<!-- The plugin is designed to update itself due to the rapid development. However, Spigot does
not provide a "good" way of doing this. Currently, the plugin checks for updates every so often,
and downloads the new file into the `plugins` directory. This means there can be several jars of
the same LEFO plugin. This is... not great.

Spigot will load the most recent plugin according to the jar file name. This means that the **file names are
part of the auto-updating system, and should not be altered.** Yes, its janky, but we don't want to have
to bother Theeno every time we update the plugin. In the future, auto updates will probably be removed. -->

## TODO
- [ ] Add a GUI
- [ ] Improve economy health telemetry
- [ ] Clean code by separating classes into separate packages
- [ ] Remove TODO comments from the code and add them here
