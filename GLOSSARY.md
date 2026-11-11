# 명언 앱 (Wise Saying App)

A console app for registering, browsing, searching, editing and deleting wise sayings, and exporting them all to one file.

## Language

**WiseSaying** (명언):
A short quotation together with the person credited for it. The single thing this app manages.
_Avoid_: Quote, saying, post, article

**Content** (명언 내용):
The text of a WiseSaying.
_Avoid_: Body, text, message

**Author** (작가):
The person a WiseSaying is credited to, stored as free text (e.g. 작자미상).
_Avoid_: Writer, user, owner

**Id** (번호):
The number that identifies a WiseSaying. Assigned once on registration, never changed, and never reused after deletion.
_Avoid_: Index, seq, no

**Export** (빌드):
A single `data.json` file holding every WiseSaying as a JSON array, produced on demand by the 빌드 command.
_Avoid_: Archive, dump, backup, build output
