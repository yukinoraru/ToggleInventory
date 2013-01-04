[![ToggleInventory](http://dev.bukkit.org/media/images/44/639/ToggleInventory.png)](http://dev.bukkit.org/server-mods/toggleinventory/)

###ToggleInventory is a plugin for multiple inventory on CraftBukkit 1.4.6.

* Demo on Youtube: [ToggleInventory - multiple inventories plugin for CraftBukkit](http://www.youtube.com/watch?&v=ZR1HcI4ro-g)

* More information: [ToggleInventory - BukkitDev](http://dev.bukkit.org/server-mods/toggleinventory/)

* 日本のフォーラム: [マインクラフト 非公式 ユーザーフォーラム](http://forum.minecraftuser.jp/viewtopic.php?f=38&t=6442)

## How to build (using Eclipse JUNO)

### Prerequisites
* Eclipse JUNO
* Maven
* JRE 1.6 lib


### Instruction
1. Clone my repo or download zip and extract.
2. Launch Eclipse. Place the ToggleInventory directory into your workspace directory.
  * Go into [File] -> [Import...]
  * Select [General] -> [Existing Projects into your Workspace] -> ....
  * Right click on ToggleInventory in 'Project Explorer', press [Maven] -> [Update Project...], hit OK.
  * Go into [File] -> [Import...]. Now, select [Run/Debug] -> [Launch Configurations]. Click 'Browse...' and browse to the project directory. Check root dir then press 'Finish'.
  * Now go to [Run]->[Run Configurations...], follow the tree to [Java Application] -> [Run Craftbukkit] and press Run.
  * Voilà, CraftBukkit has started. You can stop it like any java application. Your plugin will be automatically compiled and packaged into a jar. You can now rename things and develop your plugin.


3. To update craftbukkit or bukkit lib, you can edit 'pom.xml' from the projects list.
There is tags named 'dependency' in it like this.

		<dependency>
			<groupId>org.bukkit</groupId>
			<artifactId>bukkit</artifactId>
			<version>1.4.6-R0.3-SNAPSHOT</version>
		</dependency>
		<dependency>
			<groupId>org.bukkit</groupId>
			<artifactId>craftbukkit</artifactId>
			<version>1.4.6-R0.3-SNAPSHOT</version>
		</dependency>


If you have any problems to build this plugin, feel free to contact.

(This instruction is based on http://forums.bukkit.org/threads/eclipse-juno-maven-craftbukkit-a-plugin-project-template.90715/)


## Further Information

See [ToggleInventory - BukkitDev](http://dev.bukkit.org/server-mods/toggleinventory/).


## Plugin Statistics (mcstats.org)

[![Plugin Statistics](http://mcstats.org/signature/toggleinventory.png)](http://mcstats.org/plugin/ToggleInventory)
