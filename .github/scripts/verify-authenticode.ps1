# Verifies the Authenticode signatures of the files SignPath returned.
# Test-signing certificates are not trusted by the runner, so that trust error is accepted for the test-signing policy.
param(
  [Parameter(Mandatory = $true)] [string] $Path,
  [Parameter(Mandatory = $true)] [string] $Filter,
  [Parameter(Mandatory = $true)] [int] $ExpectedCount
)

$files = @(Get-ChildItem -Path $Path -Filter $Filter -File -Recurse)
if ($files.Count -ne $ExpectedCount) {
  throw "Expected $ExpectedCount signed '$Filter' files in $Path, found $($files.Count)."
}

$expectedSignerThumbprint = $null

foreach ($file in $files) {
  $signature = Get-AuthenticodeSignature -FilePath $file.FullName
  $status = $signature.Status.ToString()

  if ($null -eq $signature.SignerCertificate) {
    throw "$($file.Name) has no Authenticode signer certificate."
  }

  Write-Host "$($file.Name): status=$status, message=$($signature.StatusMessage)"

  $hasExpectedTestTrustError =
    $status -in @('NotTrusted', 'UnknownError') -and
    $signature.StatusMessage -match 'root certificate.*not trusted'

  if ($env:SIGNPATH_SIGNING_POLICY_SLUG -eq 'test-signing') {
    if ($status -ne 'Valid' -and -not $hasExpectedTestTrustError) {
      throw "Invalid test signature for $($file.Name): status=$status; $($signature.StatusMessage)"
    }
  }
  elseif ($status -ne 'Valid') {
    throw "Invalid release signature for $($file.Name): status=$status; $($signature.StatusMessage)"
  }

  $thumbprint = $signature.SignerCertificate.Thumbprint
  if ($null -eq $expectedSignerThumbprint) {
    $expectedSignerThumbprint = $thumbprint
  }
  elseif ($thumbprint -ne $expectedSignerThumbprint) {
    throw "The files were signed by different certificates."
  }

  Write-Host "$($file.Name): signer=$($signature.SignerCertificate.Subject), thumbprint=$thumbprint"
}
