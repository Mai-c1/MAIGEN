# 读取并解析 .env 文件
$envFile = Get-Content "d:\web\MAIGEN2\.env" -Encoding UTF8

# 存储环境变量的哈希表
$envVariables = @{}

# 第一遍：解析所有变量（不处理引用）
foreach ($line in $envFile) {
    # 去除首尾空格
    $line = $line.Trim()
    
    # 跳过注释和空行
    if ($line -eq "" -or $line.StartsWith("#")) {
        continue
    }
    
    # 分割键值对
    $parts = $line -split "=", 2
    if ($parts.Length -eq 2) {
        $key = $parts[0].Trim()
        $value = $parts[1].Trim()
        $envVariables[$key] = $value
    }
}

# 第二遍：处理变量引用并设置环境变量
foreach ($key in $envVariables.Keys) {
    $value = $envVariables[$key]
    
    # 处理变量引用
    $matches = [regex]::Matches($value, '\$\{([^}]+)\}')
    foreach ($match in $matches) {
        $refKey = $match.Groups[1].Value
        if ($envVariables.ContainsKey($refKey)) {
            $value = $value -replace "\$\{$refKey\}", $envVariables[$refKey]
        }
    }
    
    # 设置用户级环境变量
    [Environment]::SetEnvironmentVariable($key, $value, "User")
    Write-Host "已设置环境变量: $key = $value"
}

Write-Host "\n环境变量设置完成！"
Write-Host "注意：新设置的环境变量需要在新的终端窗口中才能生效。"
