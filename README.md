
# Custom Records Remake

## Description

This is a rewrite of the [CustomRecord mod for 1.12 by AshIndigo](https://github.com/AshIndigo/CustomRecord), bringing
the Forge version up to date with 1.19. With it, you can add craftable custom music discs to the game.

## Getting Started

To use this mod, first go find the `config/customrecordsremake/` directory (you may
have to launch the game first). In this directory, for each record you wish to add,
place an Ogg Vorbis file (`.ogg`) and a corresponding PNG texture file. These files
_must_ have the exact same filename (minus the extensions). So, if you wish to add
a record with a `.ogg` file named `myawesomemusic.ogg`, then the corresponding
texture must be named `myawesomemusic.png`, otherwise one of the files will not be
found.

Next, find a file named `records.json`. This file is where the definitions for
each music disc will go. To  define a new music disc, add a new entry to the
global JSON object like this:
```json
{
    "disc1": {
        "filename": "filename",
        "name": "some human-readable name",
        "length": 123,
        "item": "namespace:item",
        "meta": 0
    },
    "disc2": {
        "filename": "filename2",
        "name": "some other human-readable name",
        "length": 456,
        "item": "namespace:item2",
        "meta": 0
    }
}
```

### Descriptions of Each Record Entry Field

| Field Name | Is Required | Description                                                                                                                |
|------------|-------------|----------------------------------------------------------------------------------------------------------------------------|
| filename   | Yes         | Specifies the name of the sound and texture files for the record.                                                          |
| name       | Yes         | Specifies a human-readable name for this record.                                                                           |
| length     | Yes         | Specifies the length of the .ogg file for this record in seconds.                                                          |
| item       | No          | Specifies another item that will be used to craft this record. If omitted, then no crafting recipe will be created for it. |
| meta       | No          | Specifies a metadata value for the crafting item. If omitted, then it will be assumed to be 0.                             |

### Crafting

As long as a recipe has been added for a record, you can craft the music disc
using the following pattern:
```
 B
BIB
 B
```

Where `B` is black terracotta (stained clay), and `I` is the item specified in
the record entry.

## Note regarding compatibility with 1.12.2
This mod tries to be backwards compatible with the original mod by AshIndigo, however
due to changes in how the `RecordItem` class is constructed a new `length` field is
required for each entry.

## Credits

Credit goes to AshIndigo for the initial version of CustomRecord.
