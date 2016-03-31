const fs = require('fs')

const words = fs.readFileSync('src/sentiword_3.0.0.txt', 'utf-8')
  .split('\n')
  .map(line => line.split('\t'))
  .reduce((result, line) => {
    if (line[0].indexOf('#') == 0) return result
    const [type, id, pos, neg, wordsString = ''] = line
    if (wordsString.length <= 1) return result
    const words = wordsString
      .split(' ')
      .map(word => word.split('#')[0]) // Remove hash suffix
    words.forEach(word => {
      const separateWord = word.replace(/_/g, ' ');
      const { posStr = pos, negStr = neg } = result[separateWord] || {}
      const positive = parseFloat(posStr)
      const negative = parseFloat(negStr)
      if (positive || negative) {
        result[separateWord] =
          { positive
          , negative
          }
      }
    })
    return result
  }, {})

// console.log(JSON.stringify(words, null, 2))
Object.keys(words).forEach(word =>
  console.log(`${word}\t${words[word].positive}\t${words[word].negative}`)
)
