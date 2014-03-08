define(function() {
  describe("SimpleTest", function() {
    it('Test one should succeed', function() {
      expect(true).toBeTruthy();
    });

    it('Test two should fail', function() {
      expect(false).toBeTruthy();
    });

    it('Test three should report an error', function() {
      expect(nonExisting).toBeTruthy();
    });
  });
});
