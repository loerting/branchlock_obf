const postcss = require('postcss');

module.exports = () => {
    return {
        postcssPlugin: 'remove-comments',
        Once(root) {
            root.walkComments((comment) => {
                comment.remove();
            });
        },
    };
};

module.exports.postcss = true;
