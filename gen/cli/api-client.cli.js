// const test = JSON.parse(process.argv[2]).smartComponent)
const outputResult = async () =>
  await Promise.resolve().then(_ => process.stdout.write(
    JSON.stringify({status: 'error', message: 'Something went wrong'})
  ));

outputResult();
