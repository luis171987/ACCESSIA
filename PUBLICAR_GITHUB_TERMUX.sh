#!/data/data/com.termux/files/usr/bin/bash
# ACCESSIA — publicación pública desde Termux. Solicita login interactivo, nunca guarda un token aquí.
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"
for cmd in git gh; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Falta $cmd. Instala en Termux: pkg install git gh"
    exit 1
  fi
done
echo 'ACCESSIA será PÚBLICO y gratuito en github.com/luis171987/ACCESSIA'
echo 'Se abrirá autenticación propia de GitHub si hace falta.'
if ! gh auth status >/dev/null 2>&1; then
  gh auth login --hostname github.com --git-protocol https --web
fi
ACTUAL="$(gh api user --jq .login)"
if [ "$ACTUAL" != 'luis171987' ]; then
  echo "Cuenta autenticada: $ACTUAL; se esperaba luis171987. Cancelado para no publicar en la cuenta equivocada."
  exit 1
fi
if gh repo view 'luis171987/ACCESSIA' >/dev/null 2>&1; then
  echo 'Ya existe un repositorio ACCESSIA. No se sobrescribe ni se realiza push automático.'
  exit 1
fi
if [ ! -d .git ]; then
  git init -b main
fi
git add .
if ! git diff --cached --quiet; then
  git -c user.name='Luis Carlos Moreno Jiménez' -c user.email='luis171987@users.noreply.github.com' \
    commit -m 'feat: initialize ACCESSIA open-source accessibility prototype'
fi
gh repo create 'luis171987/ACCESSIA' --public --source=. --remote=origin --push \
  --description 'ACCESSIA: navegador inteligente y gratuito de accesibilidad universal, Android y Windows'
echo 'Consulta la URL indicada por GitHub para verificar la publicación.'
