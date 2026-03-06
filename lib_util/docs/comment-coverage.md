# lib_util Comment Coverage

Last scan: 2026-03-06
Scope: `lib_util/src/main/java/com/ail/lib_util/**/*.kt`

## Rule

- `object`/entry type has object-level KDoc
- public API has nearby method KDoc
- note: methods with annotations (`@Synchronized`, `@SuppressLint`, `@JvmName`) may be reported as false positive by simple line-based scan

## Summary

- Fully covered: 40 files
- Suggested reinforcement: 5 entries (all are annotation-adjacent false positives after manual check)

## Suggested Reinforcement (manual reviewed)

| File | Scan line | Manual result |
|---|---:|---|
| `lib_util/src/main/java/com/ail/lib_util/UtilKit.kt` | 34 | Has KDoc, flagged because `@Synchronized` is between KDoc and method |
| `lib_util/src/main/java/com/ail/lib_util/device/NetworkUtil.kt` | 37, 44 | Has KDoc, flagged because `@SuppressLint` is between KDoc and method |
| `lib_util/src/main/java/com/ail/lib_util/storage/CacheUtil.kt` | 28 | Has KDoc, flagged because `@Suppress` is between KDoc and method |
| `lib_util/src/main/java/com/ail/lib_util/storage/KvUtil.kt` | 102 | Has KDoc, flagged because `@JvmName` is between KDoc and method |

## Quick Re-Scan Script (PowerShell)

Use this in workspace root to re-check object-level and public API KDoc coverage.

```powershell
$root='E:\workspace\ASProjects\android-base-kit\lib_util\src\main\java\com\ail\lib_util'
$files=Get-ChildItem -Path $root -Recurse -Filter *.kt | Sort-Object FullName
$result=@()
foreach($f in $files){
  $lines=Get-Content $f.FullName
  $objectDoc=$true; $publicFun=0; $docFun=0; $missing=@()
  for($i=0;$i -lt $lines.Length;$i++){
    $line=$lines[$i]
    if($line -match '^\s*object\s+' -or $line -match '^\s*data\s+class\s+UtilConfig'){
      $j=$i-1; while($j -ge 0 -and $lines[$j].Trim() -eq ''){$j--}
      if($j -lt 0 -or ($lines[$j].Trim() -notmatch '^/\*\*' -and $lines[$j].Trim() -notmatch '^\*' -and $lines[$j].Trim() -notmatch '^\*/')){$objectDoc=$false}
    }
    if(($line -match '^\s*fun\s+' -or $line -match '^\s*inline\s+fun\s+') -and $line -notmatch '^\s*private\s+' -and $line -notmatch '^\s*internal\s+'){
      $publicFun++
      $j=$i-1; while($j -ge 0 -and $lines[$j].Trim() -eq ''){$j--}
      $hasDoc=($j -ge 0 -and ($lines[$j].Trim() -match '^/\*\*' -or $lines[$j].Trim() -match '^\*' -or $lines[$j].Trim() -match '^\*/' -or $lines[$j].Trim() -match '^//'))
      if($hasDoc){$docFun++} else {$missing += ($i+1)}
    }
  }
  $status = if(($publicFun -eq $docFun) -and $objectDoc){'已覆盖'} else {'建议补强'}
  $rel=$f.FullName.Replace('E:\workspace\ASProjects\android-base-kit\','').Replace('\\','/').Replace('\','/')
  $result += [PSCustomObject]@{File=$rel;ObjectDoc=$objectDoc;PublicFun=$publicFun;FunDoc=$docFun;Status=$status;MissingLines=($missing -join ', ')}
}
$result | Sort-Object Status,File | Format-Table -AutoSize
```

Tips:
- This is a fast line-based scan and may report false positives when annotation lines are between KDoc and method signatures.
- Use manual review for files listed in `建议补强`.

## Conclusion

- Current `lib_util` public API comment coverage is complete for practical usage.
- Keep following `README.md` -> `API 注释规范` for new utilities.
