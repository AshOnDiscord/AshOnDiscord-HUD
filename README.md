# AshOnDiscord HUD
In case you're wondering why the repo was archived more than over a year after I abandonded it, I just highkey forgot.
## $${\color{red}IMPORTANT}$$ $${\color{red}NOTICE}$$ 
This project is more or less **abandoned**. While there is always some sort of change I will return, I currently simply don't play minecraft or much games in general for the matter. If I even do end up returning I likely would end up starting from the scratch as the current codebase is to put it frankly, terrible. If you wish to fork this repo and build off my code(I don't know why you would want to do so), go ahead.

---

HUD/UI fabric mod for 1.19.3. Will upgrade to 1.19.4/1.20 eventually. Uses https://github.com/0x3C50/Renderer as the rendering library. Forge port not planned and likely will never come. [Showcase(with KronHud as reference)](https://www.youtube.com/watch?v=3HCbouOuGdk)

## Workspace
Don't edit the serverside code(leave empty), unless you have a hud module that benefits from it and doesn't change any serverside behavior + a toggle(in game or just in the code like a boolean).

`./run/` by default only includes the mods dir(which has some basic mods that we should probably have support for aswell are just nice to have while testing). Nothing else should be included.

## (Planned/Complete) Features
- [X] FPS
- [X] Keystrokes
- [X] CPS
- [X] Ping
- [X] Coordinates
- [ ] Server IP
- [X] Reach(Sorta implemented rn)
- [ ] Current Pack(shows name + icon of your pack)
- [ ] Time(IRL and game)
- [ ] Speed
- [ ] Sprint toggle state
- [ ] Compass/Direction
- [X] Combo
- [ ] Config Menu
- [ ] Performance mode(decrease a bunch of settings for good performance on bad computers or laptops on battery).
- more to come(ideally, basically all of kronhud, feather, and lunar hud features unless theres some other mod that does it way better like paperdoll or I'm just dumb)

## Releases
Precompiled/built jars will likely be available once the mod gets to a decent point of functionality/development.
