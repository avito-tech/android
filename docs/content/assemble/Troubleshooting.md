---
title: Troubleshooting
type: docs
---

# Troubleshooting

## Что делать в случае непонятной ошибки?

Ситуация: падает синхронизация проекта или сборка. По ошибке ничего не понятно.

Чтобы найти причину, проще всего последовательно исключать все возможные.

- Исключи влияние локальных изменений: проверь на свежем develop
- Проверь что включен VPN
- Исключи mirakle: `./mirakle.py -d`
- Исключи IDE: проверь из консоли
- Проверь не переопределено ли что-то подозрительное в `~/.gradle/gradle.properties`
- Убедись что конфигурация проекта проходит успешно: `./gradlew help`
- Посмотри детальную ошибку: `./gradlew <failed task> --stacktrace
- Исключи влияние кеширования: `./gradlew <failed task> --no-build-cache`\
Очистить кеш можно командой: `./gradlew cleanBuildCache`

### Если проблема в IDE

В консоли отработало без ошибок, но в IDE падает.

- Проверь версию IDE и Kotlin плагина. 
Возможно они слишком старые или наоборот, alpha/beta версии.
- Добавь `--stacktrace` чтобы увидеть детали ошибки:\
_Settings > Build, Execution, Deployment > Compiler > Command-line Options:_
- Проверь что не включен offline mode на вкладке Gradle
- Возможно ошибка в .iml, .idea/ файлах:
    - `./clean.py --all` или _File > Re-Import Gradle project_
    - _File > Invalidate Caches / Restart_
- Посмотри логи _Help > Show log in Finder_

### Если причина в Mirakle

Возможно из mirakle прилетают некорректные данные. Удали их: `./clean.py -r`

## Как искать проблемы с кешированием?

TBD

## Known issues

### D8: Dex file with version 'N' cannot be used with min sdk level 'M'

```none
Dex file with version '38' cannot be used with min sdk level '22'. D8
    com.android.builder.dexing.DexArchiveMergerException: Error while merging dex archives
``` 

Предположительно возникает после изменений в плагинах.\
Помогает `./clean.py -a`
