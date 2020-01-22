---
title: Mirakle
type: docs
---

# Mirakle

Это [Gradle плагин](https://github.com/Instamotor-Labs/mirakle), который переносит сборку проекта на более мощную машину в дата центре. 
Компьютер во время сборки не так нагружен, можно продолжать работать с проектом.

Как работает:

{{<mermaid>}}
sequenceDiagram
    💻->>+Builder: Держи проект (rsync)
    Note right of Builder: Собираю
    Builder-->>💻: Получи что уже готово
    Builder->>-💻: Закончил
{{</mermaid>}}


## Как настроить в первый раз?

- Установи rsync
- Посмотри какой host прописан в `mirakle.py`
- Проверь доступность удаленной машины по ssh (с VPN):\
`ssh [<username>@]<mirakle host>`\
Если нет доступа:
    - Проверь есть ли уже ключ ([checking for existing SSH keys](https://help.github.com/en/enterprise/2.15/user/articles/checking-for-existing-ssh-keys)) 
    или [сгенерируй новый](https://confluence.atlassian.com/bitbucketserver/creating-ssh-keys-776639788.html).    
    Нужен ключ без пароля, потому что из mirakle (Gradle) его некуда вводить.
    - Добавь свой публичный ssh ключ - [инструкция](http://links.k.avito.ru/QP)
    - Запроси в [Service Desk](http://links.k.avito.ru/I8) доступ по ssh на host android-builder 
- Включи mirakle: `./mirakle.py --enable`   
Если локальный пользователь отличается: `./mirakle.py --enable --username <username>`
- Проверь работу, запусти любую задачу: `./gradlew help`   
В логе будут сообщения:

```none
Here's Mirakle ...

:uploadToRemote

:executeOnRemote

:downloadInParallel
```

- Проверь **кастомные системные параметры сборки** в `~/.gradle/gradle.properties`   
Они не переносятся на удаленную машину. Выбираем один из вариантов:
    - Добавь в аналогичный файл на android-builder
    - Добавь аргументами `-Pname=value` в _Preferences | Build, Execution, Deployment | Compiler | Command-line Options_   
    NB: про такие настройки легко забыть, по возможности избегай их. 

В следующие разы нужно только включать `./mirakle.py --enable`

## Как отключить?

`./mirakle.py --disable`

Чтобы отключить только для текущей сборки, добавь в аргументы gradle `-x mirakle`   
Полное удаление настроек: `/mirakle.py --wipe`

## Troubleshooting

- Проверь что vpn подключен
- Проверь доступность mirakle, подключись по ssh 

## Known issues

### Сборка в mirakle идет дольше чем локальная

- Копирование проекта на другую машину занимает время. Если собираешь что-то небольшое, то сравни с локальной сборкой. Она может оказаться быстрее. 
- На удаленной машине накопились старые файлы в проекте, rsync их не удалил.   
Удали их: `./clean.py --remote`

### Запустилось не то, что я запускал

Проверь что синхронизация проекта проходит успешно.
