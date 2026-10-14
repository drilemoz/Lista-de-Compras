"""Regressao para o erro real no Build 26: UnsafeOptInUsageError na linha imageProxy.image.

A comprovacao definitiva pertence ao Android Lint no CI, nao a esta inspecao estatica.
"""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
source = (root / 'app/src/main/java/com/listadecompras/app/ui/BarcodeScannerActivity.kt').read_text(encoding='utf-8')
assert 'import androidx.camera.core.ExperimentalGetImage' in source
assert 'import androidx.camera.core.ImageProxy' in source
access = re.search(r'^\s*private fun readMediaImage\(imageProxy: ImageProxy\): android\.media\.Image\? = imageProxy\.image', source, re.M)
assert access, 'Acesso experimental deve ficar no metodo readMediaImage'
preceding = source[max(0, access.start()-160):access.start()]
assert re.search(r'@androidx\.annotation\.OptIn\(markerClass\s*=\s*\[ExperimentalGetImage::class\]\)\s*$', preceding), 'Usar androidx.annotation.OptIn diretamente no metodo que chama imageProxy.image'
assert len(re.findall(r'imageProxy\.image(?![A-Za-z0-9_])', source)) == 1, 'Nao acessar ImageProxy.image fora do metodo anotado'
assert 'val mediaImage = readMediaImage(imageProxy)' in source
assert '@OptIn(ExperimentalGetImage::class)' not in source, 'Kotlin OptIn nao foi reconhecido pelo Lint no Build 26'
assert '@SuppressLint("UnsafeOptInUsageError")' not in source, 'Nao ocultar erro do Lint'
# A versão é validada por scripts/validate_version.py; esta validação verifica regressões específicas.
workflow = (root / '.github/workflows/android-release.yml').read_text(encoding='utf-8')
assert 'scripts/validate_lint_build26.py' in workflow
assert 'gradle :app:testDebugUnitTest :app:assembleDebug :app:lintDebug' in workflow
print('Build 26 regression guards OK: acesso CameraX com AndroidX opt-in local, lint ativo')
